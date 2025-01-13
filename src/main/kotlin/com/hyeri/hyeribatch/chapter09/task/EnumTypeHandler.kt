package com.hyeri.hyeribatch.chapter09.task

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class EnumTypeHandler<E : Enum<E>>(private val type: Class<E>) : BaseTypeHandler<E>() {
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: E, jdbcType: JdbcType?) {
        ps.setString(i, parameter.name)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): E? {
        val value = rs.getString(columnName) ?: return null
        return java.lang.Enum.valueOf(type, value)
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): E? {
        val value = rs.getString(columnIndex) ?: return null
        return java.lang.Enum.valueOf(type, value)
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): E? {
        val value = cs.getString(columnIndex) ?: return null
        return java.lang.Enum.valueOf(type, value)
    }
}
