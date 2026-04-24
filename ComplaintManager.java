import java.util.List;
import java.util.Optional;

public class ComplaintManager {
    private final ComplaintRepository repository;

    public ComplaintManager() {
        this.repository = createRepository();
    }

    public void addComplaint(Complaint complaint) throws Exception {
        repository.add(complaint);
    }

    public List<Complaint> viewComplaints() throws Exception {
        return repository.findAll();
    }

    public Optional<Complaint> searchComplaint(int id) throws Exception {
        return repository.findById(id);
    }

    public boolean updateStatus(int id, String status) throws Exception {
        return repository.updateStatus(id, status);
    }

    public boolean deleteComplaint(int id) throws Exception {
        return repository.delete(id);
    }

    public String getStorageName() {
        return repository.getStorageName();
    }

    private ComplaintRepository createRepository() {
        try {
            return new JdbcComplaintRepository();
        } catch (Exception databaseError) {
            try {
                return new FileComplaintRepository();
            } catch (Exception fileError) {
                throw new IllegalStateException("Unable to initialize MySQL or local file storage.", fileError);
            }
        }
    }
}
