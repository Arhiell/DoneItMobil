using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DoneItAPI.Models
{
    [Table("Proyectos")]
    public class Proyecto
    {
        [Key]
        [Column("id_proyecto")]
        public int Id_Proyecto { get; set; }

        [Column("nombre")]
        public string Nombre { get; set; }

        [Column("descripcion")]
        public string? Descripcion { get; set; }

        [Column("fecha_creacion")]
        public DateTime? Fecha_Creacion { get; set; }

        [Column("id_usuario")]
        public int Id_Usuario { get; set; }

        [ForeignKey("Id_Usuario")]
        public Usuario? Usuario { get; set; }

        // 🔽 Esta línea es nueva
        public List<Tarea> Tareas { get; set; } = new();
    }
}
