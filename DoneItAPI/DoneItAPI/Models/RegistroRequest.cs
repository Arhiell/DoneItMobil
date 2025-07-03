namespace DoneItAPI.Models
{
    public class RegistroRequest
    {
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Email { get; set; }
        public string Fecha_Nacimiento { get; set; } // formato "yyyy-MM-dd"
        public string Nombre_Usuario { get; set; }
        public string Password { get; set; }
    }
}
