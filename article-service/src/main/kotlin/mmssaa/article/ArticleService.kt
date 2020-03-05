package mmssaa.article

import net.bytebuddy.utility.RandomString
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*

@EnableEurekaClient
@SpringBootApplication
class ArticleService

fun main(args: Array<String>) {
	runApplication<ArticleService>(*args)
}

@RepositoryRestResource(path = "articles")
interface ArticleRepository : JpaRepository<Article, String>

@Entity
@Table(name = "ARTICLES")
data class Article(

	@Id
	val id: String = RandomString.make(30),

	val title: String = "제목 없음",

	@Lob
	val content: String = "냉무",

	@Embedded
	@AttributeOverrides(
		value = [
			AttributeOverride(name = "id", column = Column(name = "AUTHOR_ID")),
			AttributeOverride(name = "name", column = Column(name = "AUTHOR_NAME"))
		]
	)
	val author: Account = Account(),

	@CreationTimestamp
	val createdAt: Date? = Date(),

	@UpdateTimestamp
	val updatedAt: Date? = null

)

@Embeddable
data class Account(

	val id: String,

	val name: String

) {
	constructor() : this("Unknown", "Noname")
}
