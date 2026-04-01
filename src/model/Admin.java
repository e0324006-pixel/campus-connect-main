package model;

/**
 * INHERITANCE: Admin extends User
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String adminLevel;

    public Admin(String userId, String name, String email, String password) {
        super(userId, name, email, password, "ADMIN");
        this.adminLevel = "SUPER";
        this.setApproved(true);
    }

    // POLYMORPHISM: overrides abstract method
    @Override
    public String getDashboard() {
        return "ADMIN_DASHBOARD: " + getName() + " | Level: " + adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + adminLevel;
    }

    public static Admin fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;
        Admin a = new Admin(parts[0], parts[1], parts[2], parts[3]);
        if (parts.length > 6) a.setAdminLevel(parts[6]);
        return a;
    }
}
