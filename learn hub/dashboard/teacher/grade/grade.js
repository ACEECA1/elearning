$(document).ready(function() {

    var sidebar = $('#sidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche : " + texte);
            }
        }
    });

    $('#backBtn').click(function() {
        alert("Retour au cours");
    });

    $('#downloadBtn').click(function() {
        alert("Téléchargement du devoir de l'étudiant...");
    });

    $('#submitGradeBtn').click(function() {
        var note = $('#gradeInput').val();
        var feedback = $('#feedbackInput').val();

        if (note == "") {
            alert("Veuillez saisir une note.");
        } else if (note < 0 || note > 20) {
            alert("La note doit être comprise entre 0 et 20.");
        } else {
            alert("Note de " + note + "/20 enregistrée avec le feedback.");
        }
    });

    console.log("Page de notation chargée");
});