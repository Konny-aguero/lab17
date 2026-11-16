package cr.ac.ucr.if0009.insecurepay;

import cr.ac.ucr.if0009.insecurepay.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

class ErrorHandlingTests {
    @RestController
    static class FalloControlado {
        @GetMapping("/fallo-prueba")
        public void fallo() {
            throw new RuntimeException("SQL SELECT password FROM USUARIOS; detalle interno H2");
        }
    }

    @Test
    void error500UsaProblemDetailSinInformacionInterna() throws Exception {
        MockMvcBuilders.standaloneSetup(new FalloControlado()).setControllerAdvice(new GlobalExceptionHandler())
                .build().perform(get("/fallo-prueba"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("No fue posible procesar la solicitud."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("SELECT"))))
                .andDo(result -> System.out.println("ERROR_500_VERIFICADO: " + result.getResponse().getContentAsString()));
    }
}
