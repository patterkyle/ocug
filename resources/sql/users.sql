-- :name get-users :*
select * from users

-- :name get-user :1
select * from users where id = :id

-- :name create-user :! :n
insert into users (email, password, role)
values (:email, :password, :role)

-- :name delete-user :! :n
delete from users where id = :id

-- :name change-password :! :n
update users set password = :password where id = :id

-- :name change-role :! :n
update users set role = :role where id = :id

-- :name change-activation :! :n
update users set active = :active where id = :id
