package com.mivideo.mifm.data.repositories

import com.mivideo.mifm.network.service.DuoShouService

class DuoShouRepository(val duoShouService: DuoShouService) : DuoShouService by duoShouService