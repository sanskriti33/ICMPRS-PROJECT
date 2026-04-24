import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FileComplaintRepository implements ComplaintRepository {
    private final Path filePath;

    public FileComplaintRepository() throws IOException {
        Path dataDirectory = Path.of("data");
        Files.createDirectories(dataDirectory);
        this.filePath = dataDirectory.resolve("complaints.tsv");
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
    }

    @Override
    public synchronized void add(Complaint complaint) throws Exception {
        if (findById(complaint.getId()).isPresent()) {
            throw new IllegalArgumentException("Complaint ID already exists.");
        }
        List<Complaint> complaints = findAll();
        complaints.add(complaint);
        writeAll(complaints);
    }

    @Override
    public synchronized List<Complaint> findAll() throws IOException {
        List<Complaint> complaints = new ArrayList<>();
        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                complaints.add(parse(line));
            }
        }
        complaints.sort(Comparator.comparingInt(Complaint::getPriority).reversed()
                .thenComparing(Complaint::getCreatedAt, Comparator.reverseOrder()));
        return complaints;
    }

    @Override
    public synchronized Optional<Complaint> findById(int id) throws IOException {
        return findAll().stream().filter(complaint -> complaint.getId() == id).findFirst();
    }

    @Override
    public synchronized boolean updateStatus(int id, String status) throws Exception {
        List<Complaint> complaints = findAll();
        boolean updated = false;
        for (Complaint complaint : complaints) {
            if (complaint.getId() == id) {
                complaint.setStatus(status);
                updated = true;
                break;
            }
        }
        if (updated) {
            writeAll(complaints);
        }
        return updated;
    }

    @Override
    public synchronized boolean delete(int id) throws Exception {
        List<Complaint> complaints = findAll();
        boolean removed = complaints.removeIf(complaint -> complaint.getId() == id);
        if (removed) {
            writeAll(complaints);
        }
        return removed;
    }

    @Override
    public String getStorageName() {
        return "local file fallback (" + filePath.toAbsolutePath() + ")";
    }

    private void writeAll(List<Complaint> complaints) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Complaint complaint : complaints) {
            lines.add(format(complaint));
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    private String format(Complaint complaint) {
        return String.join("\t",
                String.valueOf(complaint.getId()),
                escape(complaint.getDescription()),
                String.valueOf(complaint.getPriority()),
                escape(complaint.getDepartment()),
                escape(complaint.getStatus()),
                complaint.getCreatedAt().toString()
        );
    }

    private Complaint parse(String line) {
        String[] parts = line.split("\t", -1);
        int id = Integer.parseInt(parts[0]);
        String description = unescape(parts[1]);
        int priority = Integer.parseInt(parts[2]);
        String department = unescape(parts[3]);
        String status = unescape(parts[4]);
        LocalDateTime createdAt = LocalDateTime.parse(parts[5]);
        return new Complaint(id, description, priority, department, status, createdAt);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\");
    }
}
