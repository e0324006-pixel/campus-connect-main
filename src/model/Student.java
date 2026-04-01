package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * INHERITANCE: Student extends User
 * Demonstrates POLYMORPHISM via getDashboard() and ENCAPSULATION via private fields.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private int year;
    private double cgpa;
    private List<String> skills;
    private String resumeSummary;
    private String phone;
    private String collegeName;
    private List<String> applicationIds;
    private boolean profileComplete;

    public Student(String userId, String name, String email, String password,
                   String department, int year, double cgpa) {
        super(userId, name, email, password, "STUDENT");
        this.department = department;
        this.year = year;
        this.cgpa = cgpa;
        this.skills = new ArrayList<>();
        this.resumeSummary = "";
        this.phone = "";
        this.collegeName = "";
        this.applicationIds = new ArrayList<>();
        this.profileComplete = false;
    }

    // POLYMORPHISM: overrides abstract method
    @Override
    public String getDashboard() {
        return "STUDENT_DASHBOARD: " + getName() + " | CGPA: " + cgpa + " | Dept: " + department;
    }

    public void checkProfileComplete() {
        this.profileComplete = !resumeSummary.isEmpty() && !skills.isEmpty()
                && !phone.isEmpty() && !collegeName.isEmpty() && cgpa > 0;
    }

    public boolean isEligibleForJob(Job job) {
        return this.cgpa >= job.getMinCgpa() && this.year >= job.getMinYear();
    }

    // Skill gap analysis
    public List<String> getMissingSkills(Job job) {
        List<String> missing = new ArrayList<>();
        for (String required : job.getRequiredSkills()) {
            boolean found = false;
            for (String mySkill : this.skills) {
                if (mySkill.equalsIgnoreCase(required)) {
                    found = true;
                    break;
                }
            }
            if (!found) missing.add(required);
        }
        return missing;
    }

    public int getSkillMatchScore(Job job) {
        if (job.getRequiredSkills().isEmpty()) return 100;
        int matched = 0;
        for (String required : job.getRequiredSkills()) {
            for (String mySkill : this.skills) {
                if (mySkill.equalsIgnoreCase(required)) {
                    matched++;
                    break;
                }
            }
        }
        return (int) ((matched * 100.0) / job.getRequiredSkills().size());
    }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void addSkill(String skill) { this.skills.add(skill); }

    public String getResumeSummary() { return resumeSummary; }
    public void setResumeSummary(String resumeSummary) { this.resumeSummary = resumeSummary; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public List<String> getApplicationIds() { return applicationIds; }
    public void addApplicationId(String id) { this.applicationIds.add(id); }

    public boolean isProfileComplete() { checkProfileComplete(); return profileComplete; }

    public String toFileString() {
        String base = super.toFileString();
        String skillsStr = String.join(",", skills);
        return base + "|" + department + "|" + year + "|" + cgpa + "|" + skillsStr
                + "|" + resumeSummary + "|" + phone + "|" + collegeName;
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 12) return null;
        Student s = new Student(parts[0], parts[1], parts[2], parts[3],
                parts[6], Integer.parseInt(parts[7]), Double.parseDouble(parts[8]));
        s.setApproved(Boolean.parseBoolean(parts[5]));
        if (!parts[9].isEmpty()) {
            s.setSkills(new ArrayList<>(Arrays.asList(parts[9].split(","))));
        }
        s.setResumeSummary(parts[10]);
        s.setPhone(parts[11]);
        if (parts.length > 12) s.setCollegeName(parts[12]);
        return s;
    }
}
