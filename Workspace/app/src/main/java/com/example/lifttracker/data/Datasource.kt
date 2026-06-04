package com.example.lifttracker.data
import com.example.lifttracker.R
import com.example.lifttracker.model.Topic

class Datasource() {
    fun loadTopics(): List<Topic> {
        return listOf<Topic>(
            Topic(R.drawable.tech, R.string.tech, 118),
            Topic(R.drawable.physics, R.string.physics, 57),
            Topic(R.drawable.photography, R.string.photography, 34),
            Topic(R.drawable.painting, R.string.painting, 120),
            Topic(R.drawable.music, R.string.music, 41),
            Topic(R.drawable.lifestyle, R.string.lifestyle, 55),
            Topic(R.drawable.law, R.string.law, 21),
            Topic(R.drawable.journalism, R.string.journalism, 102),
            Topic(R.drawable.history, R.string.history, 78),
            Topic(R.drawable.geology, R.string.geology, 44),
            Topic(R.drawable.gaming, R.string.gaming, 164),
            Topic(R.drawable.finance, R.string.finance, 165),
            Topic(R.drawable.film, R.string.film, 134),
            Topic(R.drawable.fashion, R.string.fashion, 423),
            Topic(R.drawable.engineering, R.string.engineering, 78),
            Topic(R.drawable.ecology, R.string.ecology, 305),
            Topic(R.drawable.drawing, R.string.drawing, 92),
            Topic(R.drawable.design, R.string.design, 84),
            Topic(R.drawable.culinary, R.string.culinary, 73),
            Topic(R.drawable.crafts, R.string.crafts, 103),
            Topic(R.drawable.business, R.string.business, 784),
            Topic(R.drawable.biology, R.string.biology, 118),
            Topic(R.drawable.automotive, R.string.automotive, 165),
            Topic(R.drawable.architecture, R.string.architecture, 345)
        )
    }
}