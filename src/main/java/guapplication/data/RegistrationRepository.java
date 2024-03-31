package guapplication.data;

import guapplication.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    List<Registration> findAllByGenderAndStatusOrderByCreateDateAsc(String gender, Registration.Status status);

    List<Registration> findAllByStatusOrderByCreateDateAsc(Registration.Status status);

    List<Registration> findAllByUuid(UUID uuid);

    List<Registration> findAllByEmail(String email);

}