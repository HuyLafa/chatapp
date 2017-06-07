package dao

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.User

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thangle on 6/6/17.
  */
@Singleton
class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext)
      extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Users = TableQuery[UsersTable]

  def findByUsername(username: String): Future[Option[User]] =
    db.run(Users.filter(_.username === username).result.headOption)

  def insert(user: User): Future[Unit] = db.run(Users += user).map(_ => ())

  /*
   * User table
   */
  private class UsersTable(tag: Tag) extends Table[User](tag, "user") {

    def username = column[String]("USERNAME", O.PrimaryKey)
    def password = column[String]("PASSWORD")
    def email = column[String]("EMAIL")

    override def * = (username, password, email) <> (User.tupled, User.unapply)
  }
}
