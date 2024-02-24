package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

    @Test
    fun `should create a credit and return 201 status`() {
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        val creditDto: CreditDto = builderCreditDto(customerId = customer.id!!)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        //when
        //then
        mockMvc.perform(
                MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valueAsString)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(1500.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dayFirstOfInstallment").value("2024-03-12"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(6))
                .andDo(MockMvcResultHandlers.print())

    }

    @Test
    fun `should find all by customerId and return 200 status`() {
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        creditRepository.save(builderCreditDto(customerId = customer.id!!).toEntity())
        creditRepository.save(builderCreditDto2(customerId = customer.id!!).toEntity())

        //when
        //then
        mockMvc.perform(
                MockMvcRequestBuilders.get("$URL?customerId=${customer.id}")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditValue").value(1500.00))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfInstallments").value(6))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].creditValue").value(2800.00))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfInstallments").value(12))
                .andDo(MockMvcResultHandlers.print())

    }

    private fun builderCustomerDto(
        firstName: String = "Henrique",
        lastName: String = "Pedro",
        cpf: String = "573.310.710-33",
        email: String = "simba@simba.com",
        password: String = "123123",
        zipCode: String = "88302500",
        street: String = "Rua da Selva",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
    ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        income = income,
        password = password,
        zipCode = zipCode,
        street = street
    )

    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(1500.0),
        dayFirstOfInstallment: LocalDate = LocalDate.of(2024, 3, 12),
        numberOfInstallments: Int = 6,
        customerId: Long,
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId,
    )

    private fun builderCreditDto2(
        creditValue: BigDecimal = BigDecimal.valueOf(2800.0),
        dayFirstOfInstallment: LocalDate = LocalDate.of(2024, 4, 25),
        numberOfInstallments: Int = 12,
        customerId: Long,
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId,
    )
}