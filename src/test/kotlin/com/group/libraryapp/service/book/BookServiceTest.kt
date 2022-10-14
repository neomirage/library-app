package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.IllegalArgumentException

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    @AfterEach
    fun afterEach() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록 테스트")
    fun saveBookTest() {
        // given
        val bookRequest = BookRequest("kotlin")

        // when
        bookService.saveBook(bookRequest)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("kotlin")
        println(books[0].name)
    }

    @Test
    @DisplayName("책 대여 테스트 - 정상")
    fun loanBookTest() {
        // given
        bookRepository.saveAll(listOf(
            Book("kotlin"), Book("Java")
        ))

        val savedUser = userRepository.save(User("A", 20))

        val bookLoanRequest = BookLoanRequest("A", "kotlin")

        // when
        bookService.loanBook(bookLoanRequest)

        // then
        val results = userLoanHistoryRepository.findAll()

        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("kotlin")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].isReturn).isFalse
    }
    
    @Test
    @DisplayName("대출 된 책은 신규대출 실패")
    fun loanBookFailTest() {
        // given
        bookRepository.saveAll(listOf(
            Book("kotlin"), Book("Java")
        ))
        val savedUser = userRepository.save(User("A", 20))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "kotlin", false))
        val bookLoanRequest = BookLoanRequest("A", "kotlin")

        // when  & then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(bookLoanRequest)
        }.let {
            assertThat(it.message).isEqualTo("진작 대출되어 있는 책입니다")
        }
        // then    
    }

    @Test
    @DisplayName("책 반납 테스트")
    fun returnBookTest() {
        // given
        bookRepository.save(Book("kotlin"))
        val savedUser = userRepository.save(User("A", 20))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "kotlin", false))

        val bookReturnRequest = BookReturnRequest("A", "kotlin")

        // when
        bookService.returnBook(bookReturnRequest)

        // then
        val books = userLoanHistoryRepository.findAll()

        assertThat(books[0].isReturn).isTrue
    }
}