package models;

public class Admin extends User {

    public Admin(int id, String name, String email) {
        super(id, name, email, Role.ADMIN);
    }

    @Override
    public String getDisplayInfo() {
        return "Admin: " + name + " (" + email + ")";
    }
}
