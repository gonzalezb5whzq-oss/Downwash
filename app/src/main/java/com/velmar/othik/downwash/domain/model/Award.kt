package com.velmar.othik.downwash.domain.model

data class Award(val id: String, val title: String, val detail: String)

object Awards {
    val all = listOf(
        Award("first_hook", "On The Hook", "Hook your first load onto the cable."),
        Award("first_drop", "Delivered", "Set a load down on the pad."),
        Award("five_runs", "On The Roster", "Complete five missions."),
        Award("region_done", "Sector Cleared", "Complete every mission in one region."),
        Award("no_damage", "Not A Scratch", "Complete a mission with the load undamaged."),
        Award("thrifty", "Fuel Discipline", "Finish a mission with half the fuel still aboard."),
        Award("long_line", "Long Line", "Deliver a load on a cable of twelve metres or more."),
        Award("double", "Double Run", "Complete a mission carrying two loads."),
        Award("triple", "Triple Run", "Complete a mission carrying three loads."),
        Award("basket", "Litter Bearer", "Deliver a rescue basket without damaging it."),
        Award("gale", "Into The Gale", "Complete a mission in the strongest crosswind."),
        Award("all_stars", "Full Board", "Earn three stars on ten missions."),
        Award("night", "Night Rated", "Complete a mission on Night Ridge."),
        Award("grand", "Chief Pilot", "Complete every mission in the book."),
    )

    fun byId(id: String): Award? = all.firstOrNull { it.id == id }
}
