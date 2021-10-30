package dinf.backend

import dinf.exposed.postgresTestListeners
import dinf.types.ArticleID
import dinf.types.PInt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class DBArticleUseCasesTest : StringSpec({

    listeners(postgresTestListeners)

    "lists all articles" {
        val author1 = DBAuthor(createUser())
        val author2 = DBAuthor(createUser())
        author1.createArticles(10)
        author2.createArticles(30)

        val uc = DBArticleUseCases()

        uc.articles(PInt.orNull(100)!!).size shouldBe 40
    }

    "limits articles" {
        val author1 = DBAuthor(createUser())
        val author2 = DBAuthor(createUser())
        author1.createArticles(10)
        author2.createArticles(30)
        val uc = DBArticleUseCases()

        val articles = uc.articles(PInt.orNull(5)!!)

        articles.size shouldBe 5
    }

    "finds article" {
        val author = DBAuthor(createUser())
        val article = author.createArticle(content())
        val uc = DBArticleUseCases()

        val foundArticle = uc.article(article.id)

        foundArticle shouldNotBe null
    }

    "doesn't find article if it doesn't exist" {
        val uc = DBArticleUseCases()

        val foundArticle = uc.article(ArticleID(PInt.orNull(1)!!))

        foundArticle shouldBe null
    }

})