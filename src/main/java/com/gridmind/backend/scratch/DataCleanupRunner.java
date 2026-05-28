package com.gridmind.backend.scratch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.repository.UserRepository;
import com.gridmind.backend.model.User;
import java.math.BigDecimal;

@Component
public class DataCleanupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataCleanupRunner.class);

    private final EnergyConsumptionRepository consumptionRepository;
    private final UserRepository userRepository;

    public DataCleanupRunner(EnergyConsumptionRepository consumptionRepository, UserRepository userRepository) {
        this.consumptionRepository = consumptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String targetEmail = "velascoburbanocristian@gmail.com";
        userRepository.findByEmail(targetEmail).ifPresent(user -> {
            log.info("Limpiando historial antiguo para: {}", targetEmail);
            // Solo borramos si el usuario existe para no romper nada
            // Opcional: Podríamos borrar solo los registros > 1.0 si sabemos que son basura
            // Pero lo mejor es empezar de cero para que la IA calibre bien
            // consumptionRepository.deleteByUserId(user.getId()); 
            log.info("Historial listo para reinicio.");
        });
    }
}
