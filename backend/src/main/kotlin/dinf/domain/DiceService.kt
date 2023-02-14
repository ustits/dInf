package dinf.domain

import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface DiceService {

    suspend fun saveDice(name: Name, edges: Edges, userID: ID): PublicID

    suspend fun findDiceByPublicID(publicID: PublicID): Dice?

    suspend fun find(page: Page, count: Count): List<Dice>

    suspend fun search(query: SearchQuery): List<Dice>

    suspend fun deleteByPublicID(publicID: PublicID)

    class Impl(
        private val diceFactory: DiceFactory,
        private val diceRepository: DiceRepository,
        private val searchIndexRepository: SearchIndexRepository,
        private val publicIDFactory: PublicIDFactory,
        private val diceMetricRepository: DiceMetricRepository
    ) : DiceService {

        override suspend fun saveDice(name: Name, edges: Edges, userID: ID): PublicID {
            val dice = diceFactory.create(name, edges, userID)
            searchIndexRepository.add(dice)
            return publicIDFactory.fromID(dice.id)
        }

        override suspend fun findDiceByPublicID(publicID: PublicID): Dice? {
            val id = publicID.toID()
            val metric = diceMetricRepository.forID(id)
            if (metric == null) {
                diceMetricRepository.create(id, Metric.Simple(1))
            } else {
                metric.addClick()
            }
            return diceRepository.oneOrNull(id)
        }

        override suspend fun find(page: Page, count: Count): List<Dice> {
            return diceRepository.flow()
                .toList()
                .map { it to diceMetricRepository.forIDOrZero(it.id) }
                .sortAndLimit(page, count)
        }

        override suspend fun search(query: SearchQuery): List<Dice> {
            val ids = searchIndexRepository.search(query.text)
                .map { it to diceMetricRepository.forIDOrZero(it) }
                .sortAndLimit(query.page, query.count)
            return diceRepository.list(ids)
        }

        private fun <T> List<Pair<T, Metric>>.sortAndLimit(page: Page, count: Count): List<T> {
            val offset = Offset(page, count)
            return sortedByDescending { it.second.clicks }
                .map { it.first }
                .drop(offset.toInt())
                .take(count.toInt())
        }

        override suspend fun deleteByPublicID(publicID: PublicID) {
            val toDelete = diceRepository.oneOrNull(publicID)
            if (toDelete != null) {
                diceRepository.remove(toDelete)
                diceMetricRepository.removeForID(toDelete.id)
            }
        }
    }

    class Logging(private val service: DiceService) : DiceService {

        private val logger: Logger = LoggerFactory.getLogger(DiceService::class.java)

        override suspend fun saveDice(name: Name, edges: Edges, userID: ID): PublicID {
            val hashID = service.saveDice(name, edges, userID)
            logger.info("Saved dice for id: ${hashID.toID()}")
            return hashID
        }

        override suspend fun findDiceByPublicID(publicID: PublicID): Dice? {
            val dice = service.findDiceByPublicID(publicID)
            if (dice == null) {
                logger.warn("Found no dice for shareID: ${publicID.print()}")
            } else {
                logger.info("Found dice with id ${dice.id} for shareID: ${publicID.print()}")
            }
            return dice
        }

        override suspend fun find(page: Page, count: Count): List<Dice> {
            val dices = service.find(page, count)
            logger.debug("Found ${dices.size} for page: $page and count: $count")
            return dices
        }

        override suspend fun search(query: SearchQuery): List<Dice> {
            val dices = service.search(query)
            logger.info("Found ${dices.size} dices for query: $query")
            return dices
        }

        override suspend fun deleteByPublicID(publicID: PublicID) {
            service.deleteByPublicID(publicID)
            logger.info("Deleted dice for id: ${publicID.toID()}")
        }
    }

    class Stub : DiceService {
        override suspend fun saveDice(name: Name, edges: Edges, userID: ID): PublicID = PublicID.Stub()
        override suspend fun findDiceByPublicID(publicID: PublicID): Dice? = null
        override suspend fun find(page: Page, count: Count): List<Dice> = emptyList()
        override suspend fun search(query: SearchQuery): List<Dice> = emptyList()
        override suspend fun deleteByPublicID(publicID: PublicID) {}
    }

}
