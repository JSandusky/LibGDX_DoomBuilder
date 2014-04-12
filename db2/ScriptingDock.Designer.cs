namespace GdxExport {
    partial class ScriptingDock {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.btnNewScript = new System.Windows.Forms.Button();
            this.btnDeleteSel = new System.Windows.Forms.Button();
            this.grid = new System.Windows.Forms.DataGridView();
            ((System.ComponentModel.ISupportInitialize)(this.grid)).BeginInit();
            this.SuspendLayout();
            // 
            // btnNewScript
            // 
            this.btnNewScript.Location = new System.Drawing.Point(4, 4);
            this.btnNewScript.Name = "btnNewScript";
            this.btnNewScript.Size = new System.Drawing.Size(75, 23);
            this.btnNewScript.TabIndex = 0;
            this.btnNewScript.Text = "New Script";
            this.btnNewScript.UseVisualStyleBackColor = true;
            this.btnNewScript.Click += new System.EventHandler(this.btnNewScript_Click);
            // 
            // btnDeleteSel
            // 
            this.btnDeleteSel.Location = new System.Drawing.Point(172, 4);
            this.btnDeleteSel.Name = "btnDeleteSel";
            this.btnDeleteSel.Size = new System.Drawing.Size(75, 23);
            this.btnDeleteSel.TabIndex = 1;
            this.btnDeleteSel.Text = "Delete";
            this.btnDeleteSel.UseVisualStyleBackColor = true;
            this.btnDeleteSel.Click += new System.EventHandler(this.btnDeleteSel_Click);
            // 
            // grid
            // 
            this.grid.BackgroundColor = System.Drawing.SystemColors.ControlLightLight;
            this.grid.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.grid.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.grid.Location = new System.Drawing.Point(4, 34);
            this.grid.Name = "grid";
            this.grid.Size = new System.Drawing.Size(240, 305);
            this.grid.TabIndex = 2;
            // 
            // ScriptingDock
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.Controls.Add(this.grid);
            this.Controls.Add(this.btnDeleteSel);
            this.Controls.Add(this.btnNewScript);
            this.Name = "ScriptingDock";
            this.Size = new System.Drawing.Size(250, 657);
            ((System.ComponentModel.ISupportInitialize)(this.grid)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button btnNewScript;
        private System.Windows.Forms.Button btnDeleteSel;
        private System.Windows.Forms.DataGridView grid;
    }
}
