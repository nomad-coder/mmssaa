package mmssaa.account

import net.bytebuddy.utility.RandomString
import org.h2.tools.DeleteDbFiles
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.annotation.PreDestroy
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

@EnableEurekaClient
@SpringBootApplication
class AccountService

fun main(args: Array<String>) {
	runApplication<AccountService>(*args)
}

@Profile("local")
@Configuration
class LocalConfig {

	@PreDestroy
	fun removeH2DB() {
		DeleteDbFiles.execute("~/mmssaa", "account", true)
	}

}

@RefreshScope
@RestController
class TestController(
	@Value("\${say.cheese}") private val say: String
) {

	@GetMapping("/say")
	fun say() : String {
		return say
	}

}

@RestController
class AccountController(
	private val repo: AccountRepository
) {

	@PostMapping
	fun create(@Validated(ValidationGroups.Create::class) @RequestBody account: Account
			   , bindingResult: BindingResult): ResponseEntity<Account> {
		if(bindingResult.hasErrors()) {
			throw BindException(bindingResult)
		}
		return ResponseEntity.ok(repo.save(account))
	}

	@GetMapping
	fun list(): List<Account> = repo.findAll()

	@GetMapping("/{id}")
	fun details(@PathVariable id: String) = repo.findById(id).get()

	@PatchMapping("/{id}/article-count")
	fun patchArticleCount(
		@PathVariable id: String,
		@Validated(ValidationGroups.UpdateArticleCount::class) @RequestBody account: Account, bindingResult: BindingResult
	) : ResponseEntity<String> {
		if(bindingResult.hasErrors()) {
			throw BindException(bindingResult)
		}
		repo.findById(id)
			.ifPresent {
				val a = it.copy(articleCount = account.articleCount)
				repo.save(a)
			}
		return ResponseEntity.ok(account.articleCount.toString())
	}

	@DeleteMapping("/{id}") fun delete(@PathVariable id: String) = repo.deleteById(id)

}

interface AccountRepository: JpaRepository<Account, String>

@Entity
@Table(name = "ACCOUNTS")
data class Account(

	@Id
	val id: String = RandomString.make(5),

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val username: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val password: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val email: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val name: String? = null,

	@get:NotNull(groups = [ValidationGroups.UpdateArticleCount::class])
	val articleCount: Int = 0,

	val createdAt: Date? = Date()

)

object ValidationGroups {
	interface Create : Default

	interface UpdateArticleCount : Default
}