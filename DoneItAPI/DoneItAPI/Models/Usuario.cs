using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DoneItAPI.Models
{
    [Table("Usuario")]
    public class Usuario
    {
        [Key]
        [Column("id_usuario")]
        public int Id_Usuario { get; set; }

        [Column("nombre")]
        public string Nombre { get; set; }

        [Column("apellido")]
        public string Apellido { get; set; }

        [Column("email")]
        public string Email { get; set; }

        [Column("fecha_nacimiento")]
        public DateTime? Fecha_Nacimiento { get; set; }

        [Column("nombre_usuario")]
        public string Nombre_Usuario { get; set; }

        [Column("password_hash")]
        public string Password_Hash { get; set; }

        [Column("fecha_registro")]
        public DateTime? Fecha_Registro { get; set; }

        [Column("token_recuperacion")]
        public string? Token_Recuperacion { get; set; }

        [Column("vencimiento_token")]
        public DateTime? Vencimiento_Token { get; set; }
    }
}
