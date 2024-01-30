package com.adamkali.simpleide.activity

import com.adamkali.simpleide.project.Project

interface ProjectActivityListener {
    fun onProjectOpened(project: Project);
}