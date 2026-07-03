package com.sistema.questionarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação (ponto de entrada).
 *
 * A anotação @SpringBootApplication junta três configurações:
 *  - @Configuration: permite definir beans;
 *  - @EnableAutoConfiguration: o Spring Boot configura sozinho o que encontra no classpath (web, JPA, security...);
 *  - @ComponentScan: varre este pacote (com.sistema.questionarios) e subpacotes procurando
 *    @Controller, @Service, @Repository, @Component etc. para instanciar automaticamente.
 */
@SpringBootApplication
public class BackEndTrab2Application {

    // Método main padrão do Java: sobe o servidor embutido (Tomcat) e todo o contexto do Spring.
    public static void main(String[] args) {
        SpringApplication.run(BackEndTrab2Application.class, args);
    }

}
