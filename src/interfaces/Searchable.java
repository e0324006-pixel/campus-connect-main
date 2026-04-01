package interfaces;

import model.Job;
import model.Student;
import java.util.List;
import java.util.Map;

/**
 * INTERFACE: Searchable - for search/filter capabilities
 */
public interface Searchable {
    List<Job> searchJobs(Map<String, String> filters);
    List<Student> searchStudents(Map<String, String> filters);
}
