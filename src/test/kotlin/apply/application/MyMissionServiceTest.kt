package apply.application

import apply.createAssignment
import apply.createEvaluation
import apply.createEvaluationTarget
import apply.createJudgmentItem
import apply.createMission
import apply.createRecruitment
import apply.createUser
import apply.domain.assignment.AssignmentRepository
import apply.domain.evaluation.EvaluationRepository
import apply.domain.evaluationtarget.EvaluationTarget
import apply.domain.evaluationtarget.EvaluationTargetRepository
import apply.domain.judgmentitem.JudgmentItemRepository
import apply.domain.mission.MissionRepository
import apply.domain.recruitment.RecruitmentRepository
import apply.domain.user.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.springframework.transaction.annotation.Transactional
import support.test.IntegrationTest

@Transactional
@IntegrationTest
class MyMissionServiceTest(
    private val myMissionService: MyMissionService,
    private val userRepository: UserRepository,
    private val recruitmentRepository: RecruitmentRepository,
    private val evaluationRepository: EvaluationRepository,
    private val evaluationTargetRepository: EvaluationTargetRepository,
    private val missionRepository: MissionRepository,
    private val judgmentItemRepository: JudgmentItemRepository,
    private val assignmentRepository: AssignmentRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    Given("자동 채점 항목이 존재하지 않는 경우") {
        val user = userRepository.save(createUser())
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        val target = evaluationTargetRepository.save(
            createEvaluationTarget(evaluationId = evaluation.id, userId = user.id)
        )
        val mission = missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))

        When("해당 사용자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.id, recruitment.id)

            Then("예제 테스트를 실행할 수 없음을 확인할 수 있다") {
                actual shouldHaveSize 1
                actual[0].testable.shouldBeFalse()
            }
        }
    }

    fun saveEvaluationTarget(evaluationId: Long, email: String): EvaluationTarget {
        val user = userRepository.save(createUser(email = email))
        return evaluationTargetRepository.save(createEvaluationTarget(evaluationId = evaluationId, userId = user.id))
    }

    Given("과제가 존재하는 평가에 대해 여러 명의 평가 대상자가 존재하는 경우") {
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))

        saveEvaluationTarget(evaluation.id, "a@email.com")
        val target = saveEvaluationTarget(evaluation.id, "b@email.com")

        When("특정 평가 대상자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.userId, recruitment.id)

            Then("과제를 확인할 수 있다") {
                actual.shouldNotBeEmpty()
            }
        }
    }

    Given("자동 채점 항목이 있고 지원자가 과제 제출물을 제출하지 않은 경우") {
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        val mission = missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))
        judgmentItemRepository.save(createJudgmentItem(missionId = mission.id))
        val target = saveEvaluationTarget(evaluation.id, "a@email.com")

        When("해당 사용자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.userId, recruitment.id)

            Then("예제 테스트를 실행할 수 있음과 과제 제출물을 제출하지 않음을 확인할 수 있다") {
                actual shouldHaveSize 1
                actual[0].testable.shouldBeTrue()
                actual[0].submitted.shouldBeFalse()
            }
        }
    }

    Given("과제가 자동 채점 항목이 있고 지원자가 과제 제출물을 제출한 경우") {
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        val mission = missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))
        judgmentItemRepository.save(createJudgmentItem(missionId = mission.id))
        val target = saveEvaluationTarget(evaluation.id, "a@email.com")
        assignmentRepository.save(createAssignment(target.userId, mission.id))

        When("해당 사용자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.userId, recruitment.id)

            Then("예제 테스트를 실행할 수 있음과 과제 제출물을 제출 했음을 확인할 수 있다") {
                actual shouldHaveSize 1
                actual[0].testable.shouldBeTrue()
                actual[0].submitted.shouldBeTrue()
            }
        }
    }

    Given("과제가 자동 채점 항목이 없고 지원자가 과제 제출물을 제출하지 않은 경우") {
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))
        val target = saveEvaluationTarget(evaluation.id, "a@email.com")

        When("해당 사용자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.userId, recruitment.id)

            Then("예제 테스트를 실행할 수 없음과 과제 제출하지 않았음을 확인할 수 있다") {
                actual shouldHaveSize 1
                actual[0].testable.shouldBeFalse()
                actual[0].submitted.shouldBeFalse()
            }
        }
    }

    Given("과제가 자동 채점 항목이 없고 지원자가 과제 제출물을 제출한 경우") {
        val recruitment = recruitmentRepository.save(createRecruitment())
        val evaluation = evaluationRepository.save(createEvaluation(recruitmentId = recruitment.id))
        val mission = missionRepository.save(createMission(evaluationId = evaluation.id, hidden = false))
        val target = saveEvaluationTarget(evaluation.id, "a@email.com")
        assignmentRepository.save(createAssignment(target.userId, mission.id))

        When("해당 사용자의 특정 모집에 대한 모든 과제를 조회하면") {
            val actual = myMissionService.findAllByUserIdAndRecruitmentId(target.userId, recruitment.id)

            Then("예제 테스트를 실행할 수 없음과 과제 제출 했음을 확인할 수 있다") {
                actual shouldHaveSize 1
                actual[0].testable.shouldBeFalse()
                actual[0].submitted.shouldBeTrue()
            }
        }
    }
})
