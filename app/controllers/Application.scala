package controllers

import java.util.Date
import scala.io.Source
import scala.xml

import play.api._
import play.api.mvc._

// stealing liberally from
// https://gist.github.com/585235/bf328d90d094305121cec0ba2a646ce0093fa654
// http://stackoverflow.com/questions/7769454/scala-strip-all-html-tags-from-string-except-links

class Item(
            val title: String,
            val body: String,
            val link: String,
            val pubDate: String)
            
object Application extends Controller {
  
    // cleans up the RSS to correct entities and remove HTML 
    def buildItem(node: xml.Node): Item = { 
      new Item(
        (node \\ "title").text,
        (node \\ "description").text
                              .replaceAll("&nbsp;", " ")
                              .replaceAll("&copy;", "(c)")
                              .replaceAll("&raquo;", ">>")
                              .replaceAll("&quote;", "'")
                              .replaceAll("""<[^>]*?>""", " ")
                              .replaceAll("""\  +""", " ")
			      .trim,
        (node \\ "guid").text,
        (node \\ "pubDate").text
        )
    }

  // routes go below here

  def index = Action {    
    var feeds = List("NYT World News")
    
    Ok(views.html.index(feeds))
  }
  
  def feed(name:String) = Action {
      var now = new Date
      println("loading sources")
      val urls = List("http://news.google.com/news?pz=1&cf=all&ned=us&hl=en&output=rss",
                      "http://hosted2.ap.org/atom/APDEFAULT/3d281c11a96b4ad082fe88aa0db04305",
                      "http://feeds.reuters.com/Reuters/worldNews",
                      "http://rss.nytimes.com/services/xml/rss/nyt/InternationalHome.xml")
      var items = List[Item]()
      for ( url <- urls ) {
          now = new Date
          println(now.toString + "  " + url)
          val rss = Source.fromURL(url)
          val root = xml.XML.loadString(rss.mkString)
          items = (root \\ "item").map(buildItem(_)).toList ::: items
      }
      Ok(views.html.feed(name, items))
  }

}
