package dinf.backend

import dinf.domain.Dice
import dinf.exposed.DiceEntity

class DBDice(entity: DiceEntity) : Dice by Dice.Simple(
    id = entity.id.value,
    name = DBDiceName(entity),
    edges = DBEdges(entity)
)