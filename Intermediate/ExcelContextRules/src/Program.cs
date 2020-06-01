﻿using System.Diagnostics;
using System.IO;

namespace ExcelContextRules
{
	public class Program
	{
		public static void Main(string[] args)
		{
			File.Copy("template/flattening.xlsx", "flattening-out.xlsx", true);
			var data =
				new[]{
						new {player = "Cristiano Ronaldo", club = new []{new {name = "Real Madrid"}, new {name = "Manchester United"}}},
						new {player = "Lionel Messi", club = new []{new {name = "Barcelona"}}},
						new {player = "Zlatan Ibrahimović", club = new []{new {name = "Paris Saint-Germain"}, new {name = "Barcelona"}, new {name = "Inter Milan"}}}
				};
			using (var doc = NGS.Templater.Configuration.Factory.Open("flattening-out.xlsx"))
			{
				doc.Process(new { simple = data, tables = data, ranges = data });
			}
			Process.Start(new ProcessStartInfo("flattening-out.xlsx") { UseShellExecute = true });
		}
	}
}
