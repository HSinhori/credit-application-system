package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CreditServiceTest {

    @MockK lateinit var creditRepository: CreditRepository
    @MockK lateinit var customerRepository: CustomerRepository
    @MockK lateinit var customerService: CustomerService
    @InjectMockKs lateinit var creditService: CreditService

    @Test
    fun `should create credit`(){
        //given
        val fakeCustomer: Customer = buildCustomer()
        every { customerService.findById(fakeCustomer.id!!.toLong()) } returns fakeCustomer

        val fakeCredit: Credit = buildCredit(customer = fakeCustomer)
        every { creditRepository.save(any()) } returns fakeCredit

        //when
        val actual: Credit = creditService.save(fakeCredit)

        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.save(fakeCredit) }
    }

    @Test
    fun `should find credit by creditCode`() {
        //given
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCredit: Credit = buildCredit()
        val fakeCustomer: Customer = buildCustomer()
        val id = fakeCustomer.id!!.toLong()

        every { creditRepository.findByCreditCode(fakeCreditCode) } returns fakeCredit

        //when
        val actual: Credit = creditService.findByCreditCode(id, fakeCreditCode)

        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isEqualTo(fakeCredit)
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    @Test
    fun `should all credits by customer`() {
        //given
        val fakeCredit: List<Credit> = listOf(buildCredit())
        val fakeCustomer: Customer = buildCustomer()
        val id = fakeCustomer.id!!.toLong()

        every { creditRepository.findAllByCustomerId(id) } returns fakeCredit

        //when
        val actual: List<Credit> = creditService.findAllByCustomer(id)

        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isEqualTo(fakeCredit)
        verify(exactly = 1) { creditRepository.findAllByCustomerId(id) }
    }

    @Test
    fun `should not find credit by invalid creditCode and throw BusinessException`(){
        //given
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCustomer: Customer = buildCustomer()
        val id = fakeCustomer.id!!.toLong()
        every {
            creditRepository.findByCreditCode(fakeCreditCode)
        }  returns null

        //when
        //then
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
                .isThrownBy { creditService.findByCreditCode(id, fakeCreditCode) }
                .withMessage("Creditcode $fakeCreditCode not found")
        verify(exactly = 1) {creditRepository.findByCreditCode(fakeCreditCode)}
    }

    private fun buildCustomer(
        firstName: String = "Henrique",
        lastName: String = "Pedro",
        cpf: String = "573.310.710-33",
        email: String = "simba@simba.com",
        password: String = "123123",
        zipCode: String = "88302500",
        street: String = "Rua da Selva",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        id: Long = 1L
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street
        ),
        income = income,
        id = id
    )
    private fun buildCredit(
        creditCode: UUID = UUID.fromString("14160cff-1c90-424d-a532-c2df6f02d25c"),
        creditValue: BigDecimal = BigDecimal.valueOf(1500.0),
        dayFirstOfInstallment: LocalDate = LocalDate.of(2024, 3, 12),
        numberOfInstallments: Int = 6,
        status: Status = Status.IN_PROGRESS,
        customer: Customer = buildCustomer(),
        id: Long = 1L
    ) = Credit(
        creditCode = creditCode,
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        status = status,
        customer = customer,
        id = id
    )
}