package com.adamkali.simpleide.activity

import com.adamkali.simpleide.project.Project

interface ProjectActivityListener {
    fun onProjectLoad(project: Project);
}