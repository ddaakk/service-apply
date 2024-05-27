package apply.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param

fun MemberRepository.findByEmail(email: String): Member? = findBy_informationEmail(email)
fun MemberRepository.findAllByEmailIn(emails: List<String>): List<Member> = findAllBy_informationEmailIn(emails)
fun MemberRepository.existsByEmail(email: String): Boolean = existsBy_informationEmail(email)
fun MemberRepository.getOrThrow(id: Long): Member = findByIdOrNull(id)
    ?: throw NoSuchElementException("회원이 존재하지 않습니다. id: $id")

interface MemberRepository : JpaRepository<Member, Long> {
    @Query("select m from Member m where m._information.name like %:keyword% or m._information.email like %:keyword%")
    fun findAllByKeyword(@Param("keyword") keyword: String): List<Member>
    fun findBy_informationEmail(email: String): Member?
    fun findAllBy_informationEmailIn(emails: List<String>): List<Member>
    fun existsBy_informationEmail(email: String): Boolean
}
