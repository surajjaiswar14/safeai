package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.DestinationDao
import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.model.Destination
import kotlinx.coroutines.flow.Flow

class DestinationRepository(
    private val db: SafeJourneyDatabase,
    private val repositoryImpl: SafeJourneyRepository = SafeJourneyRepositoryImpl(db)
) : SafeJourneyRepository by repositoryImpl {

    // Secondary constructor for backward compatibility with existing DestinationDao injection
    constructor(dao: DestinationDao) : this(
        // Fallback to singleton database instance if constructed with DAO
        db = SafeJourneyDatabase.getDatabase(com.example.safejourneyai.SafeJourneyApplication.context)
    )

    val destinations: Flow<List<Destination>> get() = repositoryImpl.getAllDestinations()
}
