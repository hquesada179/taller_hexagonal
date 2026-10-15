package com.stagepass.live.config;

import com.stagepass.live.application.port.in.BoletoConsultaPort;
import com.stagepass.live.application.port.in.CatalogoPort;
import com.stagepass.live.application.port.in.CompraPort;
import com.stagepass.live.application.port.in.ConfirmacionPort;
import com.stagepass.live.application.port.in.ExpiracionPort;
import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.CatalogoRepository;
import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.application.port.out.FacturaPort;
import com.stagepass.live.application.port.out.FirmaPort;
import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.application.port.out.QRPort;
import com.stagepass.live.application.port.out.RelojPort;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.application.service.BoletoConsultaService;
import com.stagepass.live.application.service.CatalogoService;
import com.stagepass.live.application.service.CompraService;
import com.stagepass.live.application.service.ConfirmacionService;
import com.stagepass.live.application.service.ExpiracionService;
import com.stagepass.live.domain.policy.CalculadoraTarifas;
import com.stagepass.live.domain.policy.PoliticaEmision;
import com.stagepass.live.domain.policy.PoliticaLimite;
import com.stagepass.live.domain.policy.PoliticaReserva;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto unico de ensamblado (inyeccion por constructor) entre los puertos de
 * salida (implementados por adaptadores con anotaciones de Spring) y los
 * casos de uso del nucleo (clases Java simples, sin ninguna dependencia de
 * frameworks). Esta clase es, deliberadamente, la unica que "conoce" tanto
 * al nucleo como a Spring.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CalculadoraTarifas calculadoraTarifas() {
        return new CalculadoraTarifas();
    }

    @Bean
    public PoliticaReserva politicaReserva(@Value("${stagepass.reglas.bloqueo-minutos}") int bloqueoMinutos) {
        return new PoliticaReserva(bloqueoMinutos);
    }

    @Bean
    public PoliticaLimite politicaLimite(@Value("${stagepass.reglas.max-boletos-por-evento}") int maxBoletos) {
        return new PoliticaLimite(maxBoletos);
    }

    @Bean
    public PoliticaEmision politicaEmision() {
        return new PoliticaEmision();
    }

    @Bean
    public CompraPort compraService(CatalogoRepository catalogoRepository,
                                     ClienteRepository clienteRepository,
                                     ReservaRepository reservaRepository,
                                     OcupacionRepository ocupacionRepository,
                                     BoletoRepository boletoRepository,
                                     PagoGateway pagoGateway,
                                     RelojPort relojPort,
                                     CalculadoraTarifas calculadoraTarifas,
                                     PoliticaReserva politicaReserva,
                                     PoliticaLimite politicaLimite) {
        return new CompraService(catalogoRepository, clienteRepository, reservaRepository, ocupacionRepository,
                boletoRepository, pagoGateway, relojPort, calculadoraTarifas, politicaReserva, politicaLimite);
    }

    @Bean
    public ConfirmacionPort confirmacionService(ReservaRepository reservaRepository,
                                                 OcupacionRepository ocupacionRepository,
                                                 BoletoRepository boletoRepository,
                                                 PagoGateway pagoGateway,
                                                 ClienteRepository clienteRepository,
                                                 FirmaPort firmaPort,
                                                 NotificacionPort notificacionPort,
                                                 FacturaPort facturaPort,
                                                 RelojPort relojPort,
                                                 PoliticaEmision politicaEmision) {
        return new ConfirmacionService(reservaRepository, ocupacionRepository, boletoRepository, pagoGateway,
                clienteRepository, firmaPort, notificacionPort, facturaPort, relojPort, politicaEmision);
    }

    @Bean
    public ExpiracionPort expiracionService(ReservaRepository reservaRepository,
                                             OcupacionRepository ocupacionRepository,
                                             RelojPort relojPort) {
        return new ExpiracionService(reservaRepository, ocupacionRepository, relojPort);
    }

    @Bean
    public CatalogoPort catalogoService(CatalogoRepository catalogoRepository) {
        return new CatalogoService(catalogoRepository);
    }

    @Bean
    public BoletoConsultaPort boletoConsultaService(BoletoRepository boletoRepository, QRPort qrPort) {
        return new BoletoConsultaService(boletoRepository, qrPort);
    }
}
