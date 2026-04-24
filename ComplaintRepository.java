import java.util.List;
import java.util.Optional;

public interface ComplaintRepository {
    void add(Complaint complaint) throws Exception;

    List<Complaint> findAll() throws Exception;

    Optional<Complaint> findById(int id) throws Exception;

    boolean updateStatus(int id, String status) throws Exception;

    boolean delete(int id) throws Exception;

    String getStorageName();
}
