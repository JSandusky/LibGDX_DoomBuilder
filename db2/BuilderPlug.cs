using System;
using System.Collections.Generic;
using System.Text;
using CodeImp.DoomBuilder.Plugins;
using CodeImp.DoomBuilder;
using System.Xml;
using CodeImp.DoomBuilder.Map;

namespace GdxExport {
    public class BuilderPlug : Plug {

        public override void OnMapOpenEnd() {
            base.OnMapOpenEnd();
        }

        public override void OnMapSaveEnd(CodeImp.DoomBuilder.SavePurpose purpose) {
            base.OnMapSaveEnd(purpose);

            MapWriter writer = new MapWriter();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = true;
            settings.Encoding = Encoding.ASCII;
            settings.IndentChars = ("\t");
            settings.OmitXmlDeclaration = true;

            XmlWriter xml = XmlWriter.Create(General.Map.FilePathName + ".xml",settings);

            writer.write(General.Map.Options.LevelName, General.Map.Map, xml);
            xml.Flush();
            xml.Close();

            List<string> texList = new List<String>();
            List<int> thingList = new List<int>();

            foreach (Sidedef side in General.Map.Map.Sidedefs) {
                if (!side.LowTexture.Equals("-")) {
                    if (!texList.Contains(side.LowTexture))
                        texList.Add(side.LowTexture);
                }
                if (!side.HighTexture.Equals("-")) {
                    if (!texList.Contains(side.HighTexture))
                        texList.Add(side.HighTexture);
                }
                if (!side.MiddleTexture.Equals("-")) {
                    if (!texList.Contains(side.MiddleTexture))
                        texList.Add(side.MiddleTexture);
                }
            }

            foreach (Sector sector in General.Map.Map.Sectors) {
                if (!sector.FloorTexture.Equals("-")) {
                    if (!texList.Contains(sector.FloorTexture))
                        texList.Add(sector.FloorTexture);
                }
                if (!sector.CeilTexture.Equals("-")) {
                    if (!texList.Contains(sector.CeilTexture))
                        texList.Add(sector.CeilTexture);
                }
            }

            foreach (CodeImp.DoomBuilder.Map.Thing t in General.Map.Map.Things) {
                if (!thingList.Contains(t.Type))
                    thingList.Add(t.Type);
            }

            xml = XmlWriter.Create(General.Map.FilePathName + ".tex", settings);
            xml.WriteStartElement("resources");
            xml.WriteStartElement("textures");
            foreach (string str in texList) {
                xml.WriteElementString("tex", str);
            }
            xml.WriteEndElement();
            xml.WriteStartElement("things");
            StringBuilder sb = new StringBuilder();
            bool added = false;
            foreach (int type in thingList) {
                if (added)
                    sb.Append(",");
                added = true;
                sb.Append(type.ToString());
            }
            xml.WriteString(sb.ToString());
            xml.WriteEndElement();
            xml.WriteEndElement();
            xml.Flush();
            xml.Close();
        }
    }
}
