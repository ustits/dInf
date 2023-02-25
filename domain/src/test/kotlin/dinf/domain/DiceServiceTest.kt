package dinf.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.mockk.mockk

class DiceServiceTest : StringSpec({

    "dices with higher click count must be first" {
        val first = Dice.Stub("first")
        val second = Dice.Stub("second")
        val third = Dice.Stub("third")

        val metrics = DiceMetricRepository.InMemory(
            first to Metric.Simple(10),
            second to Metric.Simple(3),
            third to Metric.Simple(0)
        )

        val service = DiceService.Impl(
            diceFactory = mockk(),
            diceRepository = DiceRepository.Stub(listOf(first, second, third).toMutableList()),
            searchIndexRepository = SearchIndexRepository.Stub(listOf(first.id, second.id, third.id)),
            publicIDFactory = mockk(),
            diceMetricRepository = metrics,
            diceOwnerFactory = mockk()
        )

        service.search(SearchQuery("any")) shouldContainInOrder listOf(first, second, third)
    }

})