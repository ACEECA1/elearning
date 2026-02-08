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

    $('.option-item input').change(function() {
        $('.option-item').css('background-color', '');
        $('.option-item').css('border-color', '#e5e7eb');
        
        if ($(this).is(':checked')) {
            $(this).parent('.option-item').css('background-color', '#eef2ff');
            $(this).parent('.option-item').css('border-color', '#4a3bbf');
        }
    });

    $('#selectFileBtn').click(function() {
        $('#fileInput').click();
    });

    $('#fileInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Fichier sélectionné : " + fichier);
            $('.upload-subtitle').text("Fichier prêt : " + fichier);
        }
    });

    $('#uploadArea').on('dragover', function(e) {
        e.preventDefault();
        $(this).css('background-color', '#f0f0f0');
    });

    $('#uploadArea').on('dragleave', function(e) {
        e.preventDefault();
        $(this).css('background-color', '');
    });

    $('#uploadArea').on('drop', function(e) {
        e.preventDefault();
        $(this).css('background-color', '');
        alert("Veuillez utiliser le bouton 'Select File' pour télécharger.");
    });

    $('#submitQuizBtn').click(function() {
        var q1 = $('input[name="q1"]:checked').val();
        
        if (q1 == undefined) {
            alert("Veuillez répondre à toutes les questions.");
        } else {
            var score = 0;
            if (q1 == "object") {
                score = 1;
            }

            if (score == 1) {
                alert("Quiz terminé ! Votre réponse est correcte.");
            } else {
                alert("Quiz terminé. Mauvaise réponse.");
            }
        }
    });

    console.log("Quiz chargé");
});