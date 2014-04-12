using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace GdxExport {
    public class ScriptIO {
        public static void write(XmlWriter writer, LuaScript[] scripts) {
            writer.WriteStartElement("scripts");
            foreach (LuaScript lua in scripts) {
                writer.WriteStartElement("script");
                writer.WriteAttributeString("name", lua.Name);
                writer.WriteAttributeString("tag", lua.Tag.ToString());
                writer.WriteCData(lua.Code);
                writer.WriteEndElement();
            }
            writer.WriteEndElement();
        }

        public static void read(XmlDocument rdr, IList<LuaScript> scripts) {
            foreach (XmlNode nd in rdr.ChildNodes) {
                if (nd.Name == "script") {
                    LuaScript ls = new LuaScript();
                    ls.Tag = int.Parse(nd.Attributes["tag"].Value);
                    ls.Name = nd.Attributes["name"].Value;
                    ls.Code = nd.InnerText;
                    scripts.Add(ls);
                }
            }
        }
    }
}
