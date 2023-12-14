package org.example

import org.example.jooq.codegen.models.tables.records.CompanyRecord
import org.example.jooq.codegen.models.tables.references.AIRCRAFT
import org.example.jooq.codegen.models.tables.references.COMPANY
import org.example.jooq.codegen.models.tables.references.FLIGHT
import org.jooq.Insert
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import java.time.LocalDateTime


fun main() {
    println("Hello World!")
    val dslContext = DSL.using("jdbc:postgresql://localhost:5432/sandbox", "bob", "dev")
    val nextCompanyId = dslContext.select(DSL.max(COMPANY.ID)).from(COMPANY).fetchOne()?.value1()?.plus(1) ?: 1
    val insert: Insert<CompanyRecord> = dslContext.insertInto(COMPANY)
        .columns(COMPANY.ID, COMPANY.NAME, COMPANY.ADDRESS)
        .values(nextCompanyId, "SomeAirlines", "Solar system, Mars")
    val sql = insert.getSQL(ParamType.INLINED)
    println(sql)
    val insertValue = insert.execute()
    println(insertValue)

    val companies = dslContext.selectFrom(COMPANY).fetch()
    println(companies)

    val query = dslContext.select(AIRCRAFT.field(AIRCRAFT.ID))
        .from(AIRCRAFT)
        .where(AIRCRAFT.SERIAL_NUMBER.eq("123456"))
        .getSQL(ParamType.INLINED)

    println(query)

    val queryWithJoins = dslContext.select(FLIGHT.ID)
        .from(FLIGHT)
        .innerJoin(AIRCRAFT).on(AIRCRAFT.ID.eq(FLIGHT.AIRCRAFT_ID))
        .innerJoin(COMPANY).on(COMPANY.ID.eq(AIRCRAFT.COMPANY_ID))
        .where(COMPANY.NAME.eq("SomeAirlines"))
        .and(FLIGHT.DEPARTURE_AT.between(LocalDateTime.now().minusDays(1), LocalDateTime.now()))
        .getSQL(ParamType.INLINED)

    println(queryWithJoins)
}