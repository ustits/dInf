package dinf.backend

import dinf.domain.Dice
import dinf.domain.Dices
import dinf.domain.ID
import dinf.exposed.DiceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class DBDices : Dices {

    override suspend fun flow(): Flow<Dice> = newSuspendedTransaction {
        DiceEntity
            .all()
            .map { DBDice(it) }
            .asFlow()
    }

    override suspend fun create(dice: Dice) {
        newSuspendedTransaction {
            val now = Instant.now()
            DiceEntity.new {
                name = dice.name.nbString.toString()
                edges = dice.edges.stringList.toTypedArray()
                createdAt = now
                updatedAt = now
            }
        }
    }

    override suspend fun dice(id: ID): Dice? {
        return newSuspendedTransaction {
            DiceEntity.findById(id.print().toString().toInt())
        }?.let { DBDice(it) }
    }
}
