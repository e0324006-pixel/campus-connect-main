package service;

import model.Job;
import model.Student;

import java.util.*;

/**
 * Career Guidance Engine - skill gap analysis and job recommendations.
 */
public class CareerGuidanceEngine {
    private final JobService jobService;

    public CareerGuidanceEngine(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Recommend top matching jobs for a student based on skill match score.
     */
    public List<Map<String, Object>> getRecommendedJobs(Student student, int topN) {
        List<Map<String, Object>> scored = new ArrayList<>();
        for (Job job : jobService.getAllOpenJobs()) {
            int score = student.getSkillMatchScore(job);
            boolean eligible = student.isEligibleForJob(job);
            List<String> missing = student.getMissingSkills(job);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("job", job);
            entry.put("matchScore", score);
            entry.put("eligible", eligible);
            entry.put("missingSkills", missing);
            scored.add(entry);
        }
        // Sort by match score descending
        scored.sort((a, b) -> (int) b.get("matchScore") - (int) a.get("matchScore"));
        return scored.subList(0, Math.min(topN, scored.size()));
    }

    /**
     * Analyze skill gaps across all open jobs and return top missing skills.
     */
    public List<Map<String, Object>> getSkillGapAnalysis(Student student) {
        Map<String, Integer> skillFrequency = new LinkedHashMap<>();
        for (Job job : jobService.getAllOpenJobs()) {
            for (String missing : student.getMissingSkills(job)) {
                skillFrequency.merge(missing, 1, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(skillFrequency.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : entries) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("skill", e.getKey());
            row.put("jobsRequiringIt", e.getValue());
            row.put("priority", e.getValue() >= 3 ? "HIGH" : e.getValue() >= 2 ? "MEDIUM" : "LOW");
            result.add(row);
        }
        return result;
    }

    /**
     * Build JSON report for career guidance response.
     */
    public String buildGuidanceJson(Student student) {
        List<Map<String, Object>> recommended = getRecommendedJobs(student, 5);
        List<Map<String, Object>> gapAnalysis = getSkillGapAnalysis(student);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"studentName\":\"").append(student.getName()).append("\",");
        sb.append("\"cgpa\":").append(student.getCgpa()).append(",");

        // Skills
        sb.append("\"skills\":[");
        List<String> skills = student.getSkills();
        for (int i = 0; i < skills.size(); i++) {
            sb.append("\"").append(skills.get(i)).append("\"");
            if (i < skills.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Recommended jobs
        sb.append("\"recommendedJobs\":[");
        for (int i = 0; i < recommended.size(); i++) {
            Map<String, Object> entry = recommended.get(i);
            Job job = (Job) entry.get("job");
            sb.append("{");
            sb.append("\"jobId\":\"").append(job.getJobId()).append("\",");
            sb.append("\"title\":\"").append(job.getTitle()).append("\",");
            sb.append("\"company\":\"").append(job.getCompanyName()).append("\",");
            sb.append("\"matchScore\":").append(entry.get("matchScore")).append(",");
            sb.append("\"eligible\":").append(entry.get("eligible")).append(",");
            sb.append("\"missingSkills\":[");
            @SuppressWarnings("unchecked")
            List<String> missing = (List<String>) entry.get("missingSkills");
            for (int j = 0; j < missing.size(); j++) {
                sb.append("\"").append(missing.get(j)).append("\"");
                if (j < missing.size() - 1) sb.append(",");
            }
            sb.append("]}");
            if (i < recommended.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Skill gap analysis
        sb.append("\"skillGaps\":[");
        for (int i = 0; i < gapAnalysis.size(); i++) {
            Map<String, Object> g = gapAnalysis.get(i);
            sb.append("{");
            sb.append("\"skill\":\"").append(g.get("skill")).append("\",");
            sb.append("\"jobsRequiringIt\":").append(g.get("jobsRequiringIt")).append(",");
            sb.append("\"priority\":\"").append(g.get("priority")).append("\"");
            sb.append("}");
            if (i < gapAnalysis.size() - 1) sb.append(",");
        }
        sb.append("]}");

        return sb.toString();
    }
}
