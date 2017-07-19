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

  private val users = TableQuery[UsersTable]


  def list: Future[Seq[User]] = db.run(users.result)


  def findByUsername(username: String): Future[Option[User]] =
    db.run(users.filter(_.username === username).result.headOption)


  def findByEmail(email: String): Future[Option[User]] =
    db.run(users.filter(_.email === email).result.headOption)


  def findById(id: Long): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)


  def listByIds(ids: Seq[Long]): Future[Seq[User]] =
    db.run(users.filter(_.id inSet ids).result)


  def insert(user: User): Future[Unit] = db.run(users += user).map(_ => ())


  /*
   * User table
   */
  private class UsersTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")

    override def * = (id.?, username, password, email) <> (User.tupled, User.unapply)

    def usernameIndex = index("username_constraint", username, unique = true)
    def emailIndex = index("email_constraint", email, unique = true)
  }
}
