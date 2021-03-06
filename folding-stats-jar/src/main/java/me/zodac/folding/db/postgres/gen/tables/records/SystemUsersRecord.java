/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables.records;


import me.zodac.folding.db.postgres.gen.tables.SystemUsers;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SystemUsersRecord extends UpdatableRecordImpl<SystemUsersRecord> implements Record3<String, String, String[]> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.system_users.user_name</code>.
     */
    public void setUserName(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.system_users.user_name</code>.
     */
    public String getUserName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.system_users.user_password_hash</code>.
     */
    public void setUserPasswordHash(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.system_users.user_password_hash</code>.
     */
    public String getUserPasswordHash() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.system_users.roles</code>.
     */
    public void setRoles(String[] value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.system_users.roles</code>.
     */
    public String[] getRoles() {
        return (String[]) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String[]> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, String, String[]> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return SystemUsers.SYSTEM_USERS.USER_NAME;
    }

    @Override
    public Field<String> field2() {
        return SystemUsers.SYSTEM_USERS.USER_PASSWORD_HASH;
    }

    @Override
    public Field<String[]> field3() {
        return SystemUsers.SYSTEM_USERS.ROLES;
    }

    @Override
    public String component1() {
        return getUserName();
    }

    @Override
    public String component2() {
        return getUserPasswordHash();
    }

    @Override
    public String[] component3() {
        return getRoles();
    }

    @Override
    public String value1() {
        return getUserName();
    }

    @Override
    public String value2() {
        return getUserPasswordHash();
    }

    @Override
    public String[] value3() {
        return getRoles();
    }

    @Override
    public SystemUsersRecord value1(String value) {
        setUserName(value);
        return this;
    }

    @Override
    public SystemUsersRecord value2(String value) {
        setUserPasswordHash(value);
        return this;
    }

    @Override
    public SystemUsersRecord value3(String[] value) {
        setRoles(value);
        return this;
    }

    @Override
    public SystemUsersRecord values(String value1, String value2, String[] value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SystemUsersRecord
     */
    public SystemUsersRecord() {
        super(SystemUsers.SYSTEM_USERS);
    }

    /**
     * Create a detached, initialised SystemUsersRecord
     */
    public SystemUsersRecord(String userName, String userPasswordHash, String[] roles) {
        super(SystemUsers.SYSTEM_USERS);

        setUserName(userName);
        setUserPasswordHash(userPasswordHash);
        setRoles(roles);
    }
}
