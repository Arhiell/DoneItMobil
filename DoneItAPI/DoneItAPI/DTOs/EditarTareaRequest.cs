namespace DoneItAPI.DTOs
{
    public class EditarTareaRequest
    {
        public string Titulo { get; set; }
        public string? Descripcion { get; set; }
        public DateTime? Fecha_Inicio { get; set; }
        public DateTime? Fecha_Fin { get; set; }
        public string Estado { get; set; }
        public string Prioridad { get; set; }
    }
}
