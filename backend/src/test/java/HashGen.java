import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Owner: " + encoder.encode("Owner123!"));
        System.out.println("Manager: " + encoder.encode("Manager123!"));
        System.out.println("Receptionist: " + encoder.encode("Receptionist123!"));
        System.out.println("Staff: " + encoder.encode("Staff123!"));
    }
}
