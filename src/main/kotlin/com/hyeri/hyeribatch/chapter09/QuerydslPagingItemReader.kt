package com.hyeri.hyeribatch.chapter09

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.util.CollectionUtils
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function

class QuerydslPagingItemReader<T>(
    name: String = QuerydslPagingItemReader::class.simpleName!!,
    entityManagerFactory: EntityManagerFactory,
    private val querySupplier: Function<JPAQueryFactory, JPAQuery<T>>,
    private val alwaysReadFromZero: Boolean = false,
    chunkSize: Int = 1000,
) : AbstractPagingItemReader<T>() {

    init {
        pageSize = chunkSize
        super.setName(name)
    }

    private val entityManager = entityManagerFactory.createEntityManager()

    override fun doClose() {
        entityManager.close()
        super.doClose()
    }

    override fun doReadPage() {
        initQueryResult()

        val jpaQueryFactory = JPAQueryFactory(entityManager)
        val offset = if(alwaysReadFromZero) 0 else (page * pageSize).toLong()

        val query: JPAQuery<T> = querySupplier.apply(jpaQueryFactory)
            .offset(offset)
            .limit(pageSize.toLong())

        for(entity: T in query.fetch()) {
            entityManager.detach(entity)
            results.add(entity)
        }
    }

    private fun initQueryResult() {
        if(CollectionUtils.isEmpty(results)) {
            results = CopyOnWriteArrayList()
        } else {
            results.clear()
        }
    }
}