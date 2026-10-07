import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanController {
    private final Object lock = new Object();
    private final List<Scan> scans = new ArrayList<>();
    private Scan currentScan;
    private Thread worker;
    private boolean cancelRequested;
    private boolean exitRequested;

    public void handleCommand(String command) {
        String input = command == null ? "" : command.trim();
        int colon = input.indexOf(':');
        String word = colon < 0 ? input : input.substring(0, colon);
        String args = colon < 0 ? "" : input.substring(colon + 1);

        switch (word.trim().toLowerCase(Locale.ROOT)) {
            case "add" -> add(args);
            case "view" -> view();
            case "remove" -> remove(args);
            case "start" -> start();
            case "stop" -> stop();
            case "exit" -> exit();
            default -> System.out.println("Unknown command.");
        }
    }

    public boolean isExitRequested() {
        synchronized (lock) {
            return exitRequested;
        }
    }

    private void add(String args) {
        String[] parts = args.split(",", -1);
        if (parts.length != 4) {
            System.out.println("Invalid add format. Use: add:<id>, <name>, <duration>, <pause>");
            return;
        }
        Integer id = parseNumber(parts[0]);
        if (id == null) {
            System.out.println("Id must be a number.");
            return;
        }
        String name = parts[1].trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        Integer duration = parseNumber(parts[2]);
        if (duration == null || duration <= 0) {
            System.out.println("Duration must be a positive number.");
            return;
        }
        String pause = parts[3].trim();
        if (!pause.equalsIgnoreCase("Yes") && !pause.equalsIgnoreCase("No")) {
            System.out.println("Pause must be Yes or No.");
            return;
        }

        synchronized (lock) {
            if (find(id) != null) {
                System.out.println("Scan " + id + " already exists.");
                return;
            }
            scans.add(new Scan(id, name, duration, pause.equalsIgnoreCase("Yes")));
            System.out.println("Added scan " + id + ".");
        }
    }

    private void remove(String args) {
        Integer id = parseNumber(args);
        if (id == null) {
            System.out.println("Id must be a number.");
            return;
        }
        synchronized (lock) {
            Scan scan = find(id);
            if (scan == null) {
                System.out.println("Scan " + id + " not found.");
            } else if (scan.getState() != ScanState.IDLE) {
                System.out.println("Scan " + id + " has already started and cannot be removed.");
            } else {
                scans.remove(scan);
                System.out.println("Removed scan " + id + ".");
            }
        }
    }

    private void start() {
        synchronized (lock) {
            if (worker != null && worker.isAlive()) {
                System.out.println("A scan is already running.");
                return;
            }
            if (nextIdle() == null) {
                System.out.println("No scans to run.");
                return;
            }
            worker = new Thread(this::runScans);
            worker.setDaemon(true);
            worker.start();
        }
    }

    private void runScans() {
        while (true) {
            Scan scan;
            synchronized (lock) {
                if (exitRequested) {
                    return;
                }
                Thread.interrupted();
                scan = nextIdle();
                if (scan == null) {
                    return;
                }
                scan.setState(ScanState.RUNNING);
                currentScan = scan;
                cancelRequested = false;
            }
            System.out.println("Starting " + scan.getName());

            boolean interrupted = false;
            try {
                Thread.sleep(scan.getDuration() * 1000L);
            } catch (InterruptedException e) {
                interrupted = true;
            }

            synchronized (lock) {
                currentScan = null;
                if (interrupted || cancelRequested) {
                    scan.setState(ScanState.CANCELLED);
                    System.out.println("Cancelled " + scan.getName());
                } else {
                    scan.setState(ScanState.COMPLETE);
                    System.out.println("Completed " + scan.getName());
                    if (scan.isPause()) {
                        return;
                    }
                }
            }
        }
    }

    private void exit() {
        synchronized (lock) {
            exitRequested = true;
            if (worker != null) {
                worker.interrupt();
            }
        }
    }

    private void stop() {
        synchronized (lock) {
            if (currentScan == null) {
                System.out.println("No scan is running.");
                return;
            }
            cancelRequested = true;
            worker.interrupt();
        }
    }

    private void view() {
        synchronized (lock) {
            if (scans.isEmpty()) {
                System.out.println("Queue is empty.");
                return;
            }
            for (Scan scan : scans) {
                System.out.println(scan);
            }
        }
    }

    private static Integer parseNumber(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Scan nextIdle() {
        for (Scan scan : scans) {
            if (scan.getState() == ScanState.IDLE) {
                return scan;
            }
        }
        return null;
    }

    private Scan find(int id) {
        for (Scan scan : scans) {
            if (scan.getId() == id) {
                return scan;
            }
        }
        return null;
    }
}
