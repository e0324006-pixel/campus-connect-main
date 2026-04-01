package model;

import java.util.ArrayList;
import java.util.List;

/**
 * INHERITANCE: Recruiter extends User
 */
public class Recruiter extends User {
    private static final long serialVersionUID = 1L;

    private String companyName;
    private String companyDescription;
    private String industry;
    private String website;
    private String phone;
    private List<String> jobIds;

    public Recruiter(String userId, String name, String email, String password,
                     String companyName, String industry) {
        super(userId, name, email, password, "RECRUITER");
        this.companyName = companyName;
        this.industry = industry;
        this.companyDescription = "";
        this.website = "";
        this.phone = "";
        this.jobIds = new ArrayList<>();
        this.setApproved(false); // Recruiters need admin approval
    }

    // POLYMORPHISM: overrides abstract method
    @Override
    public String getDashboard() {
        return "RECRUITER_DASHBOARD: " + getName() + " | Company: " + companyName
                + " | Jobs Posted: " + jobIds.size();
    }

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getJobIds() { return jobIds; }
    public void addJobId(String id) { this.jobIds.add(id); }
    public void removeJobId(String id) { this.jobIds.remove(id); }

    @Override
    public String toFileString() {
        String base = super.toFileString();
        return base + "|" + companyName + "|" + industry + "|" + companyDescription
                + "|" + website + "|" + phone;
    }

    public static Recruiter fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 11) return null;
        Recruiter r = new Recruiter(parts[0], parts[1], parts[2], parts[3], parts[6], parts[7]);
        r.setApproved(Boolean.parseBoolean(parts[5]));
        if (parts.length > 8) r.setCompanyDescription(parts[8]);
        if (parts.length > 9) r.setWebsite(parts[9]);
        if (parts.length > 10) r.setPhone(parts[10]);
        return r;
    }
}
