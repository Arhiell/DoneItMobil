using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DoneItAPI.Models
{
    [Table("Tareas")]
    public class Tarea
    {
        [Key]
        [Column("id_tarea")]
        public int Id_Tarea { get; set; }

        [Column("titulo")]
        public string Titulo { get; set; }

        [Column("descripcion")]
        public string? Descripcion { get; set; }

        [Column("fecha_inicio")]
        public DateTime? Fecha_Inicio { get; set; }

        [Column("fecha_fin")]
        public DateTime? Fecha_Fin { get; set; }

        [Column("estado")]
        public string Estado { get; set; }

        [Column("prioridad")]
        public string Prioridad { get; set; }

        [Column("id_proyecto")]
        public int Id_Proyecto { get; set; }

       // public Proyecto? Proyecto { get; set; }
    }
}
