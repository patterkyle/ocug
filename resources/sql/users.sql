-- :name get-users :*
select * from users

-- :name get-user :1
select * from users where id = :id

-- :name create-user :<! :1
insert into users (email, password) values (:email, :password) returning id

-- :name delete-user :! :n
delete from users where id = :id

-- :name change-password :<! :1
update users set password = :new-password where id = :id returning id

-- :name change-role :<! :1
update users set user_role = :new-role where id = :id returning id

-- :name change-activation :<! :1
update users set active = :active? where id = :id returning id
